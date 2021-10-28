package com.cbdc.cbdc;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.services.VaultService;
import net.corda.core.transactions.SignedTransaction;
import com.cbdc.flows.IssueEGBPFlow;
import com.cbdc.flows.RedeemEGbpFlow;
import com.cbdc.flows.TransferEGBPFlow;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.Future;

public class CbdcFlowTests {
    CordaX500Name GBP_MINT = CordaX500Name.parse("O=UK Mint, L=London, C=GB");
    CordaX500Name COMMERCIAL_BANK = CordaX500Name.parse("O=Abbey, L=London, C=GB");
    CordaX500Name RETAIL_BANK = CordaX500Name.parse("O=HSBC, L=London, C=GB");

    private MockNetwork network;
    private StartedMockNode centralBank;
    private StartedMockNode commercialBank;//Abbey National plc
    private StartedMockNode retailBank;//HSBC UK Bank Plc

    private NetworkParameters testNetworkParameters =
            new NetworkParameters(4, Arrays.asList(), 10485760, (10485760 * 5), Instant.now(),1, new LinkedHashMap<>());
    @Before
    public void setup() {

        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.r3.corda.lib.tokens.contracts"),
                TestCordapp.findCordapp("com.r3.corda.lib.tokens.workflows"))).withNetworkParameters(testNetworkParameters));
        centralBank = network.createPartyNode(GBP_MINT);
        commercialBank = network.createPartyNode(COMMERCIAL_BANK);
        retailBank = network.createPartyNode(RETAIL_BANK);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void issueEGBP(){

        IssueEGBPFlow issueGbpFlow =
                new IssueEGBPFlow(centralBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future = centralBank.startFlow(issueGbpFlow);
        network.runNetwork();

        //get house states on ledger with uuid as input tokenId
        final TokenType gbpTokenType = new TokenType("GBP",2);
        VaultService vaultService = centralBank.getServices().getVaultService();
        Amount<TokenType> tokenType = QueryUtilities.tokenBalance(vaultService,gbpTokenType);
        System.out.println("balance with you "+tokenType.getQuantity());

    }
    @Test
    public void transferToCommercialBank(){

        IssueEGBPFlow issueGbpFlow =
                new IssueEGBPFlow(centralBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future1 = centralBank.startFlow(issueGbpFlow);
        network.runNetwork();

        TransferEGBPFlow transferGbpFlow =
                new TransferEGBPFlow(commercialBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future2 = centralBank.startFlow(transferGbpFlow);
        network.runNetwork();

        //get house states on ledger with uuid as input tokenId
        final TokenType gbpTokenType = new TokenType("GBP",2);
        VaultService vaultService = centralBank.getServices().getVaultService();
        Amount<TokenType> tokenType = QueryUtilities.tokenBalance(vaultService,gbpTokenType);
        System.out.println("balance with Central bank "+tokenType.getQuantity());


        //get house states on ledger with uuid as input tokenId
        VaultService commercialBankService = commercialBank.getServices().getVaultService();
        Amount<TokenType> commercialbankToken = QueryUtilities.tokenBalance(commercialBankService,gbpTokenType);
        System.out.println("balance with Commercial bank "+commercialbankToken.getQuantity());

    }

    @Test
    public void integrationFlow(){

        IssueEGBPFlow issueGbpFlow =
                new IssueEGBPFlow(centralBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future1 = centralBank.startFlow(issueGbpFlow);
        network.runNetwork();
        printBalanceAfterEveryOpt("#####---Central bank convert 100 GBP to E-GBP(CDBC) token--####");

        TransferEGBPFlow transferGbpFlow =
                new TransferEGBPFlow(commercialBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future2 = centralBank.startFlow(transferGbpFlow);
        network.runNetwork();
        printBalanceAfterEveryOpt("#####---Central bank transfer same CDBC to Commercial Bank --####");

        TransferEGBPFlow transferToRetail =
                new TransferEGBPFlow(retailBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future3 = commercialBank.startFlow(transferToRetail);
        network.runNetwork();
              printBalanceAfterEveryOpt("#####---Central bank transfer same Commercial to Retail Bank --####");

        TransferEGBPFlow transferToCommerialBank =
                new TransferEGBPFlow(commercialBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future4 = retailBank.startFlow(transferToCommerialBank);
        network.runNetwork();
        printBalanceAfterEveryOpt("#####---Retail bank transfer same CDBC Retail to Another Commercial Bank --####");

        TransferEGBPFlow transferToCentralBank =
                new TransferEGBPFlow(centralBank.getInfo().getLegalIdentities().get(0),100);
        Future<SignedTransaction> future5 = commercialBank.startFlow(transferToCentralBank);
        network.runNetwork();
        printBalanceAfterEveryOpt("#####---Commercial bank transfer same CDBC  to Central Bank --####");


        RedeemEGbpFlow redeemEGbpFlow =
                new RedeemEGbpFlow(100);
        Future<SignedTransaction> future6 = centralBank.startFlow(redeemEGbpFlow);
        network.runNetwork();
        printBalanceAfterEveryOpt("#####---Central Bank Redeem All CDBC ---####");
        final TokenType gbpTokenType = new TokenType("GBP",2);
        VaultService vaultService = centralBank.getServices().getVaultService();
        Amount<TokenType> tokenType = QueryUtilities.tokenBalance(vaultService,gbpTokenType);
        Assert.assertEquals("Central Bank has destroyed the token , so it should be zero",0,tokenType.getQuantity());



    }

    public void printBalanceAfterEveryOpt(String operationName){

         System.out.println(operationName);
        //get house states on ledger with uuid as input tokenId
        final TokenType gbpTokenType = new TokenType("GBP",2);
        VaultService vaultService = centralBank.getServices().getVaultService();
        Amount<TokenType> tokenType = QueryUtilities.tokenBalance(vaultService,gbpTokenType);
        System.out.println("balance with Central bank "+tokenType.getQuantity());


        //get house states on ledger with uuid as input tokenId
        VaultService commercialBankService = commercialBank.getServices().getVaultService();
        Amount<TokenType> commercialBankToken = QueryUtilities.tokenBalance(commercialBankService,gbpTokenType);
        System.out.println("balance with Commercial bank "+commercialBankToken.getQuantity());


        //get house states on ledger with uuid as input tokenId
        VaultService retailBankService = retailBank.getServices().getVaultService();
        Amount<TokenType> retailBankToken = QueryUtilities.tokenBalance(retailBankService,gbpTokenType);
        System.out.println("balance with Retail bank "+retailBankToken.getQuantity());
    }
}
