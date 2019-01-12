package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class InvoiceIssueFlow(val buyer: Party,
                       val amount: Int,
                       val status: String):FlowLogic<SignedTransaction>() {

    override val progressTracker: ProgressTracker? = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
       // Get the notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Create the output state
        val outputState = InvoiceState(ourIdentity, buyer, amount, status,
                UniqueIdentifier(),listOf(ourIdentity, buyer))

        // Building the transaction
        val transactionBuilder = TransactionBuilder(notary).
                addOutputState(outputState, InvoiceContract.ID).
                addCommand(InvoiceContract.Commands.Issue(), ourIdentity.owningKey)

        // Verify transaction Builder
        transactionBuilder.verify(serviceHub)

        // Sign the transaction
        val signedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

        // Notarize and commit
        return subFlow(FinalityFlow(signedTransaction))
    }
}