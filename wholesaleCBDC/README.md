
## Pre-Requisites

For development environment setup, please refer to: [Setup Guide](https://docs.corda.net/getting-set-up.html).


## Usage

### Running the CorDapp

Open a terminal and go to the wholesaleCBDC directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```

### Interacting with the nodes

#### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.

    Tue July 09 11:58:13 GMT 2019>>>

You can use this shell to interact with your node.
Please run the following steps in given order.

 1.Goto node : O=UK Mint, L=London, C=GB : 
  Central bank issue e-GBP (CBDC)  worth of 100 GBP
  
  `flow start IssueEGBPFlow centralBank: "O=UK Mint, L=London, C=GB",amount: 100`

2. Goto node O=UK Mint, L=London, C=GB :
    Wholesale transfer moves the CBDC from the central bank to Commercial Bank (Abbey Bank)

`flow start TransferEGBPFlow party: "O=Abbey, L=London, C=GB", amount: 100`

 3. Goto node : O=Abbey, L=London, C=GB
  Abbey Bank transfer the CBDC to Retail Bank(HSBC Bank)
  
  `flow start TransferEGBPFlow party: "O=HSBC, L=London, C=GB", amount: 100`

 4. Goto node : O=HSBC, L=London, C=GB
  Retail(HSBC) user deposited the CBDC back into the wholesale system(Abbey)
  
`flow start TransferEGBPFlow party: "O=Abbey, L=London, C=GB", amount: 100`

 5. GOTO Node :O=Abbey, L=London, C=GB 
    Wholesale transfers(Abbey) back the CBDC to the central bank(UK mint)
    
    `flow start TransferEGBPFlow party: "O=UK Mint, L=London, C=GB", amount: 100`
    
 6. GOTO node:O=UK Mint, L=London, C=GB : The central banks redeems the CBDC against the original fiat funds
 
    `flow start RedeemEGbpFlow amount: 100`
    
    you will find that all the tokens are destroyed after it has been through the wholesale payment
    system using this flow
    
    `flow start GetCBDCBalance` should show 0(zero)
    
    
 Note : you can check the balance of token on any node by running 
 
        `flow start GetCBDCBalance`
