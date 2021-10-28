package com.cbdc.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.states.FungibleToken;
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilities;
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
import net.corda.core.contracts.Amount;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;

import java.util.Collections;

@StartableByRPC
@InitiatingFlow
public class IssueEGBPFlow extends FlowLogic<SignedTransaction> {
    private final Party centralBank;
    private final long amount;

    public IssueEGBPFlow(Party centralBank, long amount) {
        this.centralBank = centralBank;
        this.amount = amount;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        final TokenType gbpTokenType = new TokenType("GBP",2);
        if (!getOurIdentity().getName().equals(UkMintConstants.GBP_MINT)) {
            throw new FlowException("We are not the UK Mint");
        }
        final IssuedTokenType ukMintGbp = new IssuedTokenType(getOurIdentity(), gbpTokenType);

        // Create a 100 gbp token that can be split and merged.
        final Amount<IssuedTokenType> amountOfUsd = AmountUtilities.amount(amount, ukMintGbp);
        final FungibleToken gbpToken = new FungibleToken(amountOfUsd, centralBank, null);

        // Issue the token to alice.
        return subFlow(new IssueTokens(
                Collections.singletonList(gbpToken), // Output instances
                Collections.emptyList())); // Observers
    }
}

