<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Invoicing CorDapp 

This is the invoicing CorDapp solution built during the PSL hackathon

**[Problem Statement](https://gist.github.com/amolpednekar/c400df847924652cd1f5c5cec0d61614)**

**This was built using the Kotlin version of the CorDapp template.**

# Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

# Usage

## Running the nodes

See https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.
    
# Extending the template

You should extend this template as follows:

* Add your own state and contract definitions under `cordapp-contracts-states/src/main/kotlin/`
* Add your own flow definitions under `cordapp/src/main/kotlin/`
* Extend or replace the client and webserver under `clients/src/main/kotlin/`

For a guided example of how to extend this template, see the Hello, World! tutorial 
[here](https://docs.corda.net/hello-world-introduction.html).

## Testing this solution

* #### Run on Seller, Buyer nodes - Should show empty vaults
    `run vaultQuery contractStateType: com.template.InvoiceState`

* #### Run on Seller node
    `flow start InvoiceIssueFlow buyer: "Buyer", amount: 500, status: "CREATED"`

* #### Run on Seller, Buyer nodes - Should show invoice created with status "CREATED"
    `run vaultQuery contractStateType: com.template.InvoiceState`

* #### Run on Buyer node - (* Use the linear ID you got by running the query above)
    `flow start InvoiceSettlementFlow linearIdentifier: 3d02ddfc-4056-42bb-beee-7ff723497748`

* #### Run on Seller, Buyer nodes - Should show invoice created with status "SETTLED"
    `run vaultQuery contractStateType: com.template.InvoiceState`

## TODOS

* Write tests for states, contracts, flows