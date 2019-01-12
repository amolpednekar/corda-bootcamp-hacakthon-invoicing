package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class InvoiceSettlementFlow(val linearIdentifier: UniqueIdentifier): FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Build the transaction
        // 1. Query invoice state by linearId
        val vaultQueryCriteria = QueryCriteria.LinearStateQueryCriteria(listOf(ourIdentity), listOf(linearIdentifier))
        val inputState = serviceHub.vaultService.queryBy<InvoiceState>(vaultQueryCriteria).states.first()

        // 2. Create new invoice state
        val outputState = inputState.state.data.copy(status = "SETTLED")

        // 3. Add command, signers as buyer and seller
        val transactionBuilder = TransactionBuilder(notary)
                .addInputState(inputState)
                .addOutputState(outputState, InvoiceContract.ID)
                .addCommand(InvoiceContract.Commands.Settle(), ourIdentity.owningKey, outputState.seller.owningKey)

        // Verify the transaction builder
        transactionBuilder.verify(serviceHub)

        // Sign the transaction
        val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Send transaction to the seller node for signing
        val otherPartySession = initiateFlow(outputState.seller)
        val completelySignedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(otherPartySession)))

        // Notarize and commit
        return subFlow(FinalityFlow(completelySignedTransaction))
    }
}

@InitiatedBy(InvoiceSettlementFlow::class)
class InvoiceSettlementResponderFlow(val otherpartySession: FlowSession): FlowLogic<Unit>(){
    @Suspendable
    override fun call() {
        val flow = object : SignTransactionFlow(otherpartySession){
            override fun checkTransaction(stx: SignedTransaction) {
                // sanity checks on this transaction
            }
        }
        subFlow(flow)
    }
}

