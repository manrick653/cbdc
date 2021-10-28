package com.cbdc.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.utilities.ProgressTracker;

public class QueryCBDC {
    @InitiatingFlow
    @StartableByRPC
    public static class GetCBDCBalance extends FlowLogic<String> {

        private final ProgressTracker progressTracker = new ProgressTracker();


        public GetCBDCBalance() {

    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {
        //get a set of the RealEstateEvolvableTokenType object on ledger with uuid as input tokenId
        final TokenType gbpTokenType = new TokenType("GBP",2);

        Amount<TokenType> tokenType = QueryUtilities.tokenBalance(getServiceHub().getVaultService(),gbpTokenType);


       // Vault.Page<FungibleToken> tokenAmountResultsClassLess = a.getServices().getVaultService().queryBy(FungibleToken.class, SelectionUtilities.tokenAmountCriteria(testTokenType));
        System.out.println("balance with you : your identity"+ getOurIdentity()+" is : "+tokenType.getQuantity());

        return "balance with you : your identity"+ getOurIdentity()+" is : "+tokenType.getQuantity();
    }
}

}
