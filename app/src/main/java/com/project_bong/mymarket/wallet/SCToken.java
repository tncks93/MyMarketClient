package com.project_bong.mymarket.wallet;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Flowable;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.5.0.
 */
@SuppressWarnings("rawtypes")
public class SCToken extends Contract {
    public static final String END_POINT = "https://sepolia.infura.io/v3/8885acd7d3c0484cb0bcfc3546e7dd62";

    public static final String CONTRACT_ADDRESS = "0x76c6395c28d6188a9ab762809173e865760e41b4";
    public static final String BINARY = "608060405260028060006101000a81548160ff021916908360ff16021790555060c86007553480156200003157600080fd5b5060405162001c8938038062001c898339818101604052810190620000579190620003af565b33600660006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508260009081620000a991906200068a565b508160019081620000bb91906200068a565b50620000fa600260009054906101000a900460ff1660ff16600a620000e19190620008f4565b82620000ee919062000945565b6200010360201b60201c565b505050620009f9565b80600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825462000154919062000990565b9250508190555080600360008282546200016f919062000990565b925050819055503373ffffffffffffffffffffffffffffffffffffffff16600073ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef83604051620001d69190620009dc565b60405180910390a350565b6000604051905090565b600080fd5b600080fd5b600080fd5b600080fd5b6000601f19601f8301169050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6200024a82620001ff565b810181811067ffffffffffffffff821117156200026c576200026b62000210565b5b80604052505050565b600062000281620001e1565b90506200028f82826200023f565b919050565b600067ffffffffffffffff821115620002b257620002b162000210565b5b620002bd82620001ff565b9050602081019050919050565b60005b83811015620002ea578082015181840152602081019050620002cd565b60008484015250505050565b60006200030d620003078462000294565b62000275565b9050828152602081018484840111156200032c576200032b620001fa565b5b62000339848285620002ca565b509392505050565b600082601f830112620003595762000358620001f5565b5b81516200036b848260208601620002f6565b91505092915050565b6000819050919050565b620003898162000374565b81146200039557600080fd5b50565b600081519050620003a9816200037e565b92915050565b600080600060608486031215620003cb57620003ca620001eb565b5b600084015167ffffffffffffffff811115620003ec57620003eb620001f0565b5b620003fa8682870162000341565b935050602084015167ffffffffffffffff8111156200041e576200041d620001f0565b5b6200042c8682870162000341565b92505060406200043f8682870162000398565b9150509250925092565b600081519050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b600060028204905060018216806200049c57607f821691505b602082108103620004b257620004b162000454565b5b50919050565b60008190508160005260206000209050919050565b60006020601f8301049050919050565b600082821b905092915050565b6000600883026200051c7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82620004dd565b620005288683620004dd565b95508019841693508086168417925050509392505050565b6000819050919050565b60006200056b620005656200055f8462000374565b62000540565b62000374565b9050919050565b6000819050919050565b62000587836200054a565b6200059f620005968262000572565b848454620004ea565b825550505050565b600090565b620005b6620005a7565b620005c38184846200057c565b505050565b5b81811015620005eb57620005df600082620005ac565b600181019050620005c9565b5050565b601f8211156200063a576200060481620004b8565b6200060f84620004cd565b810160208510156200061f578190505b620006376200062e85620004cd565b830182620005c8565b50505b505050565b600082821c905092915050565b60006200065f600019846008026200063f565b1980831691505092915050565b60006200067a83836200064c565b9150826002028217905092915050565b620006958262000449565b67ffffffffffffffff811115620006b157620006b062000210565b5b620006bd825462000483565b620006ca828285620005ef565b600060209050601f831160018114620007025760008415620006ed578287015190505b620006f985826200066c565b86555062000769565b601f1984166200071286620004b8565b60005b828110156200073c5784890151825560018201915060208501945060208101905062000715565b868310156200075c578489015162000758601f8916826200064c565b8355505b6001600288020188555050505b505050505050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b60008160011c9050919050565b6000808291508390505b6001851115620007ff57808604811115620007d757620007d662000771565b5b6001851615620007e75780820291505b8081029050620007f785620007a0565b9450620007b7565b94509492505050565b6000826200081a5760019050620008ed565b816200082a5760009050620008ed565b81600181146200084357600281146200084e5762000884565b6001915050620008ed565b60ff84111562000863576200086262000771565b5b8360020a9150848211156200087d576200087c62000771565b5b50620008ed565b5060208310610133831016604e8410600b8410161715620008be5782820a905083811115620008b857620008b762000771565b5b620008ed565b620008cd8484846001620007ad565b92509050818404811115620008e757620008e662000771565b5b81810290505b9392505050565b6000620009018262000374565b91506200090e8362000374565b92506200093d7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff848462000808565b905092915050565b6000620009528262000374565b91506200095f8362000374565b92508282026200096f8162000374565b9150828204841483151762000989576200098862000771565b5b5092915050565b60006200099d8262000374565b9150620009aa8362000374565b9250828201905080821115620009c557620009c462000771565b5b92915050565b620009d68162000374565b82525050565b6000602082019050620009f36000830184620009cb565b92915050565b6112808062000a096000396000f3fe6080604052600436106100c65760003560e01c806331b0a2c61161007f5780638da5cb5b116100595780638da5cb5b146104dc57806395d89b4114610507578063a9059cbb14610532578063dd62ed3e1461056f5761030e565b806331b0a2c61461044b57806342966c681461047657806370a082311461049f5761030e565b806306fdde0314610313578063095ea7b31461033e57806318160ddd1461037b57806323b872dd146103a657806327e235e3146103e3578063313ce567146104205761030e565b3661030e57600034036100d857600080fd5b6000600754346100e89190610e11565b90508060046000600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054101561015857600080fd5b8060046000600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282546101c99190610e53565b9250508190555080600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825461021f9190610e87565b92505081905550600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff160361028557610284816105ac565b5b3373ffffffffffffffffffffffffffffffffffffffff16600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040516103049190610eca565b60405180910390a3005b600080fd5b34801561031f57600080fd5b50610328610684565b6040516103359190610f75565b60405180910390f35b34801561034a57600080fd5b5061036560048036038101906103609190611026565b610712565b6040516103729190611081565b60405180910390f35b34801561038757600080fd5b50610390610804565b60405161039d9190610eca565b60405180910390f35b3480156103b257600080fd5b506103cd60048036038101906103c8919061109c565b61080a565b6040516103da9190611081565b60405180910390f35b3480156103ef57600080fd5b5061040a600480360381019061040591906110ef565b610a44565b6040516104179190610eca565b60405180910390f35b34801561042c57600080fd5b50610435610a5c565b6040516104429190611138565b60405180910390f35b34801561045757600080fd5b50610460610a6f565b60405161046d9190610eca565b60405180910390f35b34801561048257600080fd5b5061049d60048036038101906104989190611153565b610a75565b005b3480156104ab57600080fd5b506104c660048036038101906104c191906110ef565b610b4d565b6040516104d39190610eca565b60405180910390f35b3480156104e857600080fd5b506104f1610b96565b6040516104fe919061118f565b60405180910390f35b34801561051357600080fd5b5061051c610bbc565b6040516105299190610f75565b60405180910390f35b34801561053e57600080fd5b5061055960048036038101906105549190611026565b610c4a565b6040516105669190611081565b60405180910390f35b34801561057b57600080fd5b50610596600480360381019061059191906111aa565b610db3565b6040516105a39190610eca565b60405180910390f35b80600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282546105fb9190610e87565b9250508190555080600360008282546106149190610e87565b925050819055503373ffffffffffffffffffffffffffffffffffffffff16600073ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040516106799190610eca565b60405180910390a350565b6000805461069190611219565b80601f01602080910402602001604051908101604052809291908181526020018280546106bd90611219565b801561070a5780601f106106df5761010080835404028352916020019161070a565b820191906000526020600020905b8154815290600101906020018083116106ed57829003601f168201915b505050505081565b600081600560003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040516107f29190610eca565b60405180910390a36001905092915050565b60035481565b600081600560008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054101561089557600080fd5b81600560008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282546109219190610e53565b9250508190555081600460008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282546109779190610e53565b9250508190555081600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282546109cd9190610e87565b925050819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef84604051610a319190610eca565b60405180910390a3600190509392505050565b60046020528060005260406000206000915090505481565b600260009054906101000a900460ff1681565b60075481565b80600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254610ac49190610e53565b925050819055508060036000828254610add9190610e53565b92505081905550600073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef83604051610b429190610eca565b60405180910390a350565b6000600460008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020549050919050565b600660009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054610bc990611219565b80601f0160208091040260200160405190810160405280929190818152602001828054610bf590611219565b8015610c425780601f10610c1757610100808354040283529160200191610c42565b820191906000526020600020905b815481529060010190602001808311610c2557829003601f168201915b505050505081565b600081600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015610c9857600080fd5b81600460003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254610ce79190610e53565b9250508190555081600460008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000828254610d3d9190610e87565b925050819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef84604051610da19190610eca565b60405180910390a36001905092915050565b6005602052816000526040600020602052806000526040600020600091509150505481565b6000819050919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610e1c82610dd8565b9150610e2783610dd8565b9250828202610e3581610dd8565b91508282048414831517610e4c57610e4b610de2565b5b5092915050565b6000610e5e82610dd8565b9150610e6983610dd8565b9250828203905081811115610e8157610e80610de2565b5b92915050565b6000610e9282610dd8565b9150610e9d83610dd8565b9250828201905080821115610eb557610eb4610de2565b5b92915050565b610ec481610dd8565b82525050565b6000602082019050610edf6000830184610ebb565b92915050565b600081519050919050565b600082825260208201905092915050565b60005b83811015610f1f578082015181840152602081019050610f04565b60008484015250505050565b6000601f19601f8301169050919050565b6000610f4782610ee5565b610f518185610ef0565b9350610f61818560208601610f01565b610f6a81610f2b565b840191505092915050565b60006020820190508181036000830152610f8f8184610f3c565b905092915050565b600080fd5b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000610fc782610f9c565b9050919050565b610fd781610fbc565b8114610fe257600080fd5b50565b600081359050610ff481610fce565b92915050565b61100381610dd8565b811461100e57600080fd5b50565b60008135905061102081610ffa565b92915050565b6000806040838503121561103d5761103c610f97565b5b600061104b85828601610fe5565b925050602061105c85828601611011565b9150509250929050565b60008115159050919050565b61107b81611066565b82525050565b60006020820190506110966000830184611072565b92915050565b6000806000606084860312156110b5576110b4610f97565b5b60006110c386828701610fe5565b93505060206110d486828701610fe5565b92505060406110e586828701611011565b9150509250925092565b60006020828403121561110557611104610f97565b5b600061111384828501610fe5565b91505092915050565b600060ff82169050919050565b6111328161111c565b82525050565b600060208201905061114d6000830184611129565b92915050565b60006020828403121561116957611168610f97565b5b600061117784828501611011565b91505092915050565b61118981610fbc565b82525050565b60006020820190506111a46000830184611180565b92915050565b600080604083850312156111c1576111c0610f97565b5b60006111cf85828601610fe5565b92505060206111e085828601610fe5565b9150509250929050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b6000600282049050600182168061123157607f821691505b602082108103611244576112436111ea565b5b5091905056fea2646970667358221220b808b2e0054de199ee930e8fa9d12fc163bee419bb2211a9f1bcadc88c426b6664736f6c63430008120033";

    public static final String FUNC_ALLOWANCE = "allowance";

    public static final String FUNC_APPROVE = "approve";

    public static final String FUNC_BALANCEOF = "balanceOf";

    public static final String FUNC_BALANCES = "balances";

    public static final String FUNC_BURN = "burn";

    public static final String FUNC_DECIMALS = "decimals";

    public static final String FUNC_ETHCANBUY = "ethCanBuy";

    public static final String FUNC_NAME = "name";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_SYMBOL = "symbol";

    public static final String FUNC_TOTALSUPPLY = "totalSupply";

    public static final String FUNC_TRANSFER = "transfer";

    public static final String FUNC_TRANSFERFROM = "transferFrom";

    public static final Event APPROVAL_EVENT = new Event("Approval", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TRANSFER_EVENT = new Event("Transfer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected SCToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SCToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SCToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SCToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

//    public static List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
//        ArrayList<ApprovalEventResponse> responses = new ArrayList<ApprovalEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            ApprovalEventResponse typedResponse = new ApprovalEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
//            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static ApprovalEventResponse getApprovalEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(APPROVAL_EVENT, log);
        ApprovalEventResponse typedResponse = new ApprovalEventResponse();
        typedResponse.log = log;
        typedResponse.owner = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.spender = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getApprovalEventFromLog(log));
    }

    public Flowable<ApprovalEventResponse> approvalEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
        return approvalEventFlowable(filter);
    }

//    public static List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
//
//        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
//        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
//        for (Contract.EventValuesWithLog eventValues : valueList) {
//            TransferEventResponse typedResponse = new TransferEventResponse();
//            typedResponse.log = eventValues.getLog();
//            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
//            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
//            typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
//            responses.add(typedResponse);
//        }
//        return responses;
//    }

    public static TransferEventResponse getTransferEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(TRANSFER_EVENT, log);
        TransferEventResponse typedResponse = new TransferEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getTransferEventFromLog(log));
    }

    public Flowable<TransferEventResponse> transferEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
        return transferEventFlowable(filter);
    }

    public RemoteFunctionCall<BigInteger> allowance(String param0, String param1) {
        final Function function = new Function(FUNC_ALLOWANCE, 
                Arrays.<Type>asList(new Address(160, param0),
                new Address(160, param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> approve(String spender, BigInteger amount) {
        final Function function = new Function(
                FUNC_APPROVE, 
                Arrays.<Type>asList(new Address(160, spender),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> balanceOf(String account) {
        final Function function = new Function(FUNC_BALANCEOF, 
                Arrays.<Type>asList(new Address(160, account)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> balances(String param0) {
        final Function function = new Function(FUNC_BALANCES, 
                Arrays.<Type>asList(new Address(160, param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> burn(BigInteger amount) {
        final Function function = new Function(
                FUNC_BURN, 
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> decimals() {
        final Function function = new Function(FUNC_DECIMALS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<BigInteger> ethCanBuy() {
        final Function function = new Function(FUNC_ETHCANBUY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> name() {
        final Function function = new Function(FUNC_NAME, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> symbol() {
        final Function function = new Function(FUNC_SYMBOL, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> totalSupply() {
        final Function function = new Function(FUNC_TOTALSUPPLY, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transfer(String recipient, BigInteger amount) {
        final Function function = new Function(
                FUNC_TRANSFER, 
                Arrays.<Type>asList(new Address(160, recipient),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferFrom(String sender, String recipient, BigInteger amount) {
        final Function function = new Function(
                FUNC_TRANSFERFROM, 
                Arrays.<Type>asList(new Address(160, sender),
                new Address(160, recipient),
                new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SCToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SCToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SCToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SCToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SCToken load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SCToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SCToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SCToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SCToken> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, String _name, String _symbol, BigInteger _amount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_name),
                new Utf8String(_symbol),
                new Uint256(_amount)));
        return deployRemoteCall(SCToken.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<SCToken> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, String _name, String _symbol, BigInteger _amount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_name),
                new Utf8String(_symbol),
                new Uint256(_amount)));
        return deployRemoteCall(SCToken.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<SCToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _name, String _symbol, BigInteger _amount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_name),
                new Utf8String(_symbol),
                new Uint256(_amount)));
        return deployRemoteCall(SCToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<SCToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _name, String _symbol, BigInteger _amount) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Utf8String(_name),
                new Utf8String(_symbol),
                new Uint256(_amount)));
        return deployRemoteCall(SCToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class ApprovalEventResponse extends BaseEventResponse {
        public String owner;

        public String spender;

        public BigInteger value;
    }

    public static class TransferEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger value;
    }
}
