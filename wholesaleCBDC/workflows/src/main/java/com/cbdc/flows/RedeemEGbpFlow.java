package com.cbdc.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilities;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.workflows.flows.rpc.RedeemFungibleTokens;
import com.r3.corda.lib.tokens.workflows.utilities.QueryUtilities;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;

import java.util.Collections;

@InitiatingFlow
@StartableByRPC
public class RedeemEGbpFlow extends FlowLogic<SignedTransaction> {

    private final long amount ;

    public RedeemEGbpFlow(long amount) {
        this.amount = amount;
    }


    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final TokenType usdTokenType = FiatCurrency.Companion.getInstance("GBP");
        final Party usMint = getServiceHub().getNetworkMapCache().getPeerByLegalName( UkMintConstants.GBP_MINT);
        if (usMint == null) throw new FlowException("No UK Mint found");

        // Describe how to find those GBP held by Me.
        final QueryCriteria heldByMe = QueryUtilities.heldTokenAmountCriteria(usdTokenType, getOurIdentity());
        final Amount<TokenType> usdAmount = AmountUtilities.amount(amount, usdTokenType);

        // Do the redeem
        return subFlow(new RedeemFungibleTokens(
                usdAmount, // How much to redeem
                usMint, // issuer
                Collections.emptyList(), // Observers
                heldByMe, // Criteria to find the inputs
                getOurIdentity())); // change holder
    }

}
