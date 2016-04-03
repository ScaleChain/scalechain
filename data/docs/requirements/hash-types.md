# Introduction
This document analyzes the different hash types used by Bitcoin core.

## (P0) implement different hash types
https://bitcoin.org/en/developer-guide#signature-hash-types

### src/rpc/rawtransaction.cpp
The signrawtransaction RPC receives 6 kinds of sighashtypes.
```
UniValue signrawtransaction(const UniValue& params, bool fHelp) {
   ...
            "4. \"sighashtype\"     (string, optional, default=ALL) The signature hash type. Must be one of\n"
            "       \"ALL\"\n"
            "       \"NONE\"\n"
            "       \"SINGLE\"\n"
            "       \"ALL|ANYONECANPAY\"\n"
            "       \"NONE|ANYONECANPAY\"\n"
            "       \"SINGLE|ANYONECANPAY\"\n"

   ...
   
    int nHashType = SIGHASH_ALL;
    if (params.size() > 3 && !params[3].isNull()) {
        static map<string, int> mapSigHashValues =
            boost::assign::map_list_of
            (string("ALL"), int(SIGHASH_ALL))
            (string("ALL|ANYONECANPAY"), int(SIGHASH_ALL|SIGHASH_ANYONECANPAY))
            (string("NONE"), int(SIGHASH_NONE))
            (string("NONE|ANYONECANPAY"), int(SIGHASH_NONE|SIGHASH_ANYONECANPAY))
            (string("SINGLE"), int(SIGHASH_SINGLE))
            (string("SINGLE|ANYONECANPAY"), int(SIGHASH_SINGLE|SIGHASH_ANYONECANPAY))
            ;
        string strHashType = params[3].get_str();
        if (mapSigHashValues.count(strHashType))
            nHashType = mapSigHashValues[strHashType];

   ...
   
    for (unsigned int i = 0; i < mergedTx.vin.size(); i++) {
        CTxIn& txin = mergedTx.vin[i];
        const CCoins* coins = view.AccessCoins(txin.prevout.hash);
        if (coins == NULL || !coins->IsAvailable(txin.prevout.n)) {
            TxInErrorToJSON(txin, vErrors, "Input not found or already spent");
            continue;
        }
        const CScript& prevPubKey = coins->vout[txin.prevout.n].scriptPubKey;

        txin.scriptSig.clear();
        // Only sign SIGHASH_SINGLE if there's a corresponding output:
        if (!fHashSingle || (i < mergedTx.vout.size()))
            SignSignature(keystore, prevPubKey, mergedTx, i, nHashType);   
            
   ...          
```

### src/script/sign.cpp
The hash type is passed to SignSignature.
```
bool SignSignature(const CKeyStore &keystore, const CScript& fromPubKey, CMutableTransaction& txTo, unsigned int nIn, int nHashType)
{
    assert(nIn < txTo.vin.size());
    CTxIn& txin = txTo.vin[nIn];

    CTransaction txToConst(txTo);
    TransactionSignatureCreator creator(&keystore, &txToConst, nIn, nHashType);

    return ProduceSignature(creator, fromPubKey, txin.scriptSig);
}
```

The ProduceSignature function:
```
bool ProduceSignature(const BaseSignatureCreator& creator, const CScript& fromPubKey, CScript& scriptSig)
{
    txnouttype whichType;
    if (!SignStep(creator, fromPubKey, scriptSig, whichType))
        return false;

    if (whichType == TX_SCRIPTHASH)
    {
        // Solver returns the subscript that need to be evaluated;
        // the final scriptSig is the signatures from that
        // and then the serialized subscript:
        CScript subscript = scriptSig;

        txnouttype subType;
        bool fSolved =
            SignStep(creator, subscript, scriptSig, subType) && subType != TX_SCRIPTHASH;
        // Append serialized subscript whether or not it is completely signed:
        scriptSig << valtype(subscript.begin(), subscript.end());
        if (!fSolved) return false;
    }

    // Test solution
    return VerifyScript(scriptSig, fromPubKey, STANDARD_SCRIPT_VERIFY_FLAGS, creator.Checker());
}

```

In SignStep function :
```
/**
 * Sign scriptPubKey using signature made with creator.
 * Signatures are returned in scriptSigRet (or returns false if scriptPubKey can't be signed),
 * unless whichTypeRet is TX_SCRIPTHASH, in which case scriptSigRet is the redemption script.
 * Returns false if scriptPubKey could not be completely satisfied.
 */
static bool SignStep(const BaseSignatureCreator& creator, const CScript& scriptPubKey,
                     CScript& scriptSigRet, txnouttype& whichTypeRet)
{
    scriptSigRet.clear();

    vector<valtype> vSolutions;
    if (!Solver(scriptPubKey, whichTypeRet, vSolutions))
        return false;

    CKeyID keyID;
    switch (whichTypeRet)
    {
    case TX_NONSTANDARD:
    case TX_NULL_DATA:
        return false;
    case TX_PUBKEY:
        keyID = CPubKey(vSolutions[0]).GetID();
        return Sign1(keyID, creator, scriptPubKey, scriptSigRet);
    case TX_PUBKEYHASH:
        keyID = CKeyID(uint160(vSolutions[0]));
        if (!Sign1(keyID, creator, scriptPubKey, scriptSigRet))
            return false;
        else
        {
            CPubKey vch;
            creator.KeyStore().GetPubKey(keyID, vch);
            scriptSigRet << ToByteVector(vch);
        }
        return true;
    case TX_SCRIPTHASH:
        return creator.KeyStore().GetCScript(uint160(vSolutions[0]), scriptSigRet);

    case TX_MULTISIG:
        scriptSigRet << OP_0; // workaround CHECKMULTISIG bug
        return (SignN(vSolutions, creator, scriptPubKey, scriptSigRet));
    }
    return false;
}
```

### src/script/standard.cpp
The Solver function decides the type of the locking script. The SignStep function branches based on the type of the locking script.
```
/**
 * Return public keys or hashes from scriptPubKey, for 'standard' transaction types.
 */
bool Solver(const CScript& scriptPubKey, txnouttype& typeRet, vector<vector<unsigned char> >& vSolutionsRet)
{
    // Templates
    static multimap<txnouttype, CScript> mTemplates;
    if (mTemplates.empty())
    {
        // Standard tx, sender provides pubkey, receiver adds signature
        mTemplates.insert(make_pair(TX_PUBKEY, CScript() << OP_PUBKEY << OP_CHECKSIG));

        // Bitcoin address tx, sender provides hash of pubkey, receiver provides signature and pubkey
        mTemplates.insert(make_pair(TX_PUBKEYHASH, CScript() << OP_DUP << OP_HASH160 << OP_PUBKEYHASH << OP_EQUALVERIFY << OP_CHECKSIG));

        // Sender provides N pubkeys, receivers provides M signatures
        mTemplates.insert(make_pair(TX_MULTISIG, CScript() << OP_SMALLINTEGER << OP_PUBKEYS << OP_SMALLINTEGER << OP_CHECKMULTISIG));
    }

    vSolutionsRet.clear();

    // Shortcut for pay-to-script-hash, which are more constrained than the other types:
    // it is always OP_HASH160 20 [20 byte hash] OP_EQUAL
    if (scriptPubKey.IsPayToScriptHash())
    {
        typeRet = TX_SCRIPTHASH;
        vector<unsigned char> hashBytes(scriptPubKey.begin()+2, scriptPubKey.begin()+22);
        vSolutionsRet.push_back(hashBytes);
        return true;
    }

    // Provably prunable, data-carrying output
    //
    // So long as script passes the IsUnspendable() test and all but the first
    // byte passes the IsPushOnly() test we don't care what exactly is in the
    // script.
    if (scriptPubKey.size() >= 1 && scriptPubKey[0] == OP_RETURN && scriptPubKey.IsPushOnly(scriptPubKey.begin()+1)) {
        typeRet = TX_NULL_DATA;
        return true;
    }

    // Scan templates
    const CScript& script1 = scriptPubKey;
    BOOST_FOREACH(const PAIRTYPE(txnouttype, CScript)& tplate, mTemplates)
    {
        const CScript& script2 = tplate.second;
        vSolutionsRet.clear();

        opcodetype opcode1, opcode2;
        vector<unsigned char> vch1, vch2;

        // Compare
        CScript::const_iterator pc1 = script1.begin();
        CScript::const_iterator pc2 = script2.begin();
        while (true)
        {
            if (pc1 == script1.end() && pc2 == script2.end())
            {
                // Found a match
                typeRet = tplate.first;
                if (typeRet == TX_MULTISIG)
                {
                    // Additional checks for TX_MULTISIG:
                    unsigned char m = vSolutionsRet.front()[0];
                    unsigned char n = vSolutionsRet.back()[0];
                    if (m < 1 || n < 1 || m > n || vSolutionsRet.size()-2 != n)
                        return false;
                }
                return true;
            }
            if (!script1.GetOp(pc1, opcode1, vch1))
                break;
            if (!script2.GetOp(pc2, opcode2, vch2))
                break;

            // Template matching opcodes:
            if (opcode2 == OP_PUBKEYS)
            {
                while (vch1.size() >= 33 && vch1.size() <= 65)
                {
                    vSolutionsRet.push_back(vch1);
                    if (!script1.GetOp(pc1, opcode1, vch1))
                        break;
                }
                if (!script2.GetOp(pc2, opcode2, vch2))
                    break;
                // Normal situation is to fall through
                // to other if/else statements
            }

            if (opcode2 == OP_PUBKEY)
            {
                if (vch1.size() < 33 || vch1.size() > 65)
                    break;
                vSolutionsRet.push_back(vch1);
            }
            else if (opcode2 == OP_PUBKEYHASH)
            {
                if (vch1.size() != sizeof(uint160))
                    break;
                vSolutionsRet.push_back(vch1);
            }
            else if (opcode2 == OP_SMALLINTEGER)
            {   // Single-byte small integer pushed onto vSolutions
                if (opcode1 == OP_0 ||
                    (opcode1 >= OP_1 && opcode1 <= OP_16))
                {
                    char n = (char)CScript::DecodeOP_N(opcode1);
                    vSolutionsRet.push_back(valtype(1, n));
                }
                else
                    break;
            }
            else if (opcode1 != opcode2 || vch1 != vch2)
            {
                // Others must match exactly
                break;
            }
        }
    }

    vSolutionsRet.clear();
    typeRet = TX_NONSTANDARD;
    return false;
}
```
### src/script/sign.cpp

When signing a transaction input, we need to put the hash type at the end of the signature.
```
bool TransactionSignatureCreator::CreateSig(std::vector<unsigned char>& vchSig, const CKeyID& address, const CScript& scriptCode) const
{
    CKey key;
    if (!keystore->GetKey(address, key))
        return false;

    uint256 hash = SignatureHash(scriptCode, *txTo, nIn, nHashType);
    if (!key.Sign(hash, vchSig))
        return false;
    vchSig.push_back((unsigned char)nHashType);
    return true;
}
```

### src/script/interpreter.cpp
SignatureHash uses CTransactionSignatureSerializer to serialize a transaction based on the hash type.
```
uint256 SignatureHash(const CScript& scriptCode, const CTransaction& txTo, unsigned int nIn, int nHashType)
{
    static const uint256 one(uint256S("0000000000000000000000000000000000000000000000000000000000000001"));
    if (nIn >= txTo.vin.size()) {
        //  nIn out of range
        return one;
    }

    // Check for invalid use of SIGHASH_SINGLE
    if ((nHashType & 0x1f) == SIGHASH_SINGLE) {
        if (nIn >= txTo.vout.size()) {
            //  nOut out of range
            return one;
        }
    }

    // Wrapper to serialize only the necessary parts of the transaction being signed
    CTransactionSignatureSerializer txTmp(txTo, scriptCode, nIn, nHashType);

    // Serialize and hash
    CHashWriter ss(SER_GETHASH, 0);
    ss << txTmp << nHashType;
    return ss.GetHash();
}
```

Finally, CTransactionSignatureSerializer serializes a transaction based on the hash type.
Note that we are creating a hash from the serialized transaction, and 
we are using different ways to serialize a transaction based on the hash type.
```
/**
 * Wrapper that serializes like CTransaction, but with the modifications
 *  required for the signature hash done in-place
 */
class CTransactionSignatureSerializer {
private:
    const CTransaction &txTo;  //! reference to the spending transaction (the one being serialized)
    const CScript &scriptCode; //! output script being consumed
    const unsigned int nIn;    //! input index of txTo being signed
    const bool fAnyoneCanPay;  //! whether the hashtype has the SIGHASH_ANYONECANPAY flag set
    const bool fHashSingle;    //! whether the hashtype is SIGHASH_SINGLE
    const bool fHashNone;      //! whether the hashtype is SIGHASH_NONE

public:
    CTransactionSignatureSerializer(const CTransaction &txToIn, const CScript &scriptCodeIn, unsigned int nInIn, int nHashTypeIn) :
        txTo(txToIn), scriptCode(scriptCodeIn), nIn(nInIn),
        fAnyoneCanPay(!!(nHashTypeIn & SIGHASH_ANYONECANPAY)),
        fHashSingle((nHashTypeIn & 0x1f) == SIGHASH_SINGLE),
        fHashNone((nHashTypeIn & 0x1f) == SIGHASH_NONE) {}

    /** Serialize the passed scriptCode, skipping OP_CODESEPARATORs */
    template<typename S>
    void SerializeScriptCode(S &s, int nType, int nVersion) const {
        CScript::const_iterator it = scriptCode.begin();
        CScript::const_iterator itBegin = it;
        opcodetype opcode;
        unsigned int nCodeSeparators = 0;
        while (scriptCode.GetOp(it, opcode)) {
            if (opcode == OP_CODESEPARATOR)
                nCodeSeparators++;
        }
        ::WriteCompactSize(s, scriptCode.size() - nCodeSeparators);
        it = itBegin;
        while (scriptCode.GetOp(it, opcode)) {
            if (opcode == OP_CODESEPARATOR) {
                s.write((char*)&itBegin[0], it-itBegin-1);
                itBegin = it;
            }
        }
        if (itBegin != scriptCode.end())
            s.write((char*)&itBegin[0], it-itBegin);
    }

    /** Serialize an input of txTo */
    template<typename S>
    void SerializeInput(S &s, unsigned int nInput, int nType, int nVersion) const {
        // In case of SIGHASH_ANYONECANPAY, only the input being signed is serialized
        if (fAnyoneCanPay)
            nInput = nIn;
        // Serialize the prevout
        ::Serialize(s, txTo.vin[nInput].prevout, nType, nVersion);
        // Serialize the script
        if (nInput != nIn)
            // Blank out other inputs' signatures
            ::Serialize(s, CScriptBase(), nType, nVersion);
        else
            SerializeScriptCode(s, nType, nVersion);
        // Serialize the nSequence
        if (nInput != nIn && (fHashSingle || fHashNone))
            // let the others update at will
            ::Serialize(s, (int)0, nType, nVersion);
        else
            ::Serialize(s, txTo.vin[nInput].nSequence, nType, nVersion);
    }

    /** Serialize an output of txTo */
    template<typename S>
    void SerializeOutput(S &s, unsigned int nOutput, int nType, int nVersion) const {
        if (fHashSingle && nOutput != nIn)
            // Do not lock-in the txout payee at other indices as txin
            ::Serialize(s, CTxOut(), nType, nVersion);
        else
            ::Serialize(s, txTo.vout[nOutput], nType, nVersion);
    }

    /** Serialize txTo */
    template<typename S>
    void Serialize(S &s, int nType, int nVersion) const {
        // Serialize nVersion
        ::Serialize(s, txTo.nVersion, nType, nVersion);
        // Serialize vin
        unsigned int nInputs = fAnyoneCanPay ? 1 : txTo.vin.size();
        ::WriteCompactSize(s, nInputs);
        for (unsigned int nInput = 0; nInput < nInputs; nInput++)
             SerializeInput(s, nInput, nType, nVersion);
        // Serialize vout
        unsigned int nOutputs = fHashNone ? 0 : (fHashSingle ? nIn+1 : txTo.vout.size());
        ::WriteCompactSize(s, nOutputs);
        for (unsigned int nOutput = 0; nOutput < nOutputs; nOutput++)
             SerializeOutput(s, nOutput, nType, nVersion);
        // Serialize nLockTime
        ::Serialize(s, txTo.nLockTime, nType, nVersion);
    }
};
```

### src/wallet/wallet.cpp
CWallet::CreateTransaction uses TransactionSignatureCreator for creating a signature for a transaction.
```
bool CWallet::CreateTransaction(const vector<CRecipient>& vecSend, CWalletTx& wtxNew, CReserveKey& reservekey, CAmount& nFeeRet,
                                int& nChangePosRet, std::string& strFailReason, const CCoinControl* coinControl, bool sign)
{
    ...
    BOOST_FOREACH(const PAIRTYPE(const CWalletTx*,unsigned int)& coin, setCoins)
    {
        bool signSuccess;
        const CScript& scriptPubKey = coin.first->vout[coin.second].scriptPubKey;
        CScript& scriptSigRes = txNew.vin[nIn].scriptSig;
        if (sign)
            signSuccess = ProduceSignature(TransactionSignatureCreator(this, &txNewConst, nIn, SIGHASH_ALL), scriptPubKey, scriptSigRes);
        else
            signSuccess = ProduceSignature(DummySignatureCreator(this), scriptPubKey, scriptSigRes);
    ...
}
```