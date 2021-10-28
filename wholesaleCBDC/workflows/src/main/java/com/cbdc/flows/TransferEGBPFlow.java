package com.cbdc.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.r3.corda.lib.tokens.contracts.types.TokenType;
import com.r3.corda.lib.tokens.contracts.utilities.AmountUtilities;
import com.r3.corda.lib.tokens.money.FiatCurrency;
import com.r3.corda.lib.tokens.workflows.flows.rpc.MoveFungibleTokens;
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount;
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
public class TransferEGBPFlow extends FlowLogic<SignedTransaction> {

    private final Party party;
    private final long amount;

    public TransferEGBPFlow(Party party, long amount) {
        this.party = party;
        this.amount = amount;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // Prepare what we are talking about.
        final TokenType gbpTokenType = FiatCurrency.Companion.getInstance("GBP");
        final Party gbpMint = getServiceHub().getNetworkMapCache().getPeerByLegalName( UkMintConstants.GBP_MINT);
        if (gbpMint == null) throw new FlowException("No uk Mint found");

        // Who is going to own the output, and how much?
        final Amount<TokenType> usdAmount = AmountUtilities.amount(amount, gbpTokenType);
        final PartyAndAmount<TokenType> bobsAmount = new PartyAndAmount<>(party, usdAmount);

        // Describe how to find those $ held by Me.
        final QueryCriteria issuedByUkMint = QueryUtilities.tokenAmountWithIssuerCriteria(gbpTokenType, gbpMint);
        final QueryCriteria heldByMe = QueryUtilities.heldTokenAmountCriteria(gbpTokenType, getOurIdentity());

        // Do the move
        return subFlow(new MoveFungibleTokens(
                Collections.singletonList(bobsAmount), // Output instances
                Collections.emptyList(), // Observers
                issuedByUkMint.and(heldByMe), // Criteria to find the inputs
                getOurIdentity())); // change holder
    }

}
