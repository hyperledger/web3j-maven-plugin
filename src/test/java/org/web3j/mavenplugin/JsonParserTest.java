package org.web3j.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JsonParserTest {


    JsonParser jsonParser = new JsonParser();

    @Test(expected = MojoExecutionException.class)
    public void parseJsonInValidJson() throws MojoExecutionException {

        String exampleJson = "Im not a json string";

        jsonParser.parseJson(exampleJson);

        fail("Exception should be thrown");
    }

    @Test
    public void parseJsonValidJson() throws MojoExecutionException {

        String exampleJson = "{\"contracts\":{\"C:\\\\source\\\\blockchain_fg\\\\mavenplugin\\\\web3j-maven-plugin\\\\src\\\\test\\\\resources\\\\Greeter.sol:greeter\":{\"abi\":\"[{\\\"constant\\\":false,\\\"inputs\\\":[],\\\"name\\\":\\\"kill\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"greet\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"view\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"_greeting\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}]\",\"bin\":\"608060405234801561001057600080fd5b506040516102d43803806102d483398101604052805160008054600160a060020a0319163317905501805161004c906001906020840190610053565b50506100ee565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009457805160ff19168380011785556100c1565b828001600101855582156100c1579182015b828111156100c15782518255916020019190600101906100a6565b506100cd9291506100d1565b5090565b6100eb91905b808211156100cd57600081556001016100d7565b90565b6101d7806100fd6000396000f30060806040526004361061004b5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166341c0e1b58114610050578063cfae321714610067575b600080fd5b34801561005c57600080fd5b506100656100f1565b005b34801561007357600080fd5b5061007c610116565b6040805160208082528351818301528351919283929083019185019080838360005b838110156100b657818101518382015260200161009e565b50505050905090810190601f1680156100e35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60005473ffffffffffffffffffffffffffffffffffffffff163314156101145733ff5b565b60018054604080516020601f600260001961010087891615020190951694909404938401819004810282018101909252828152606093909290918301828280156101a15780601f10610176576101008083540402835291602001916101a1565b820191906000526020600020905b81548152906001019060200180831161018457829003601f168201915b50505050509050905600a165627a7a72305820cc6098304090a0138949db8eaa6c32735e873ce74d2e513674c63c7add935dd70029\",\"metadata\":\"{\\\"compiler\\\":{\\\"version\\\":\\\"0.4.24+commit.6ae8fb59\\\"},\\\"language\\\":\\\"Solidity\\\",\\\"output\\\":{\\\"abi\\\":[{\\\"constant\\\":false,\\\"inputs\\\":[],\\\"name\\\":\\\"kill\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"greet\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"view\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"_greeting\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}],\\\"devdoc\\\":{\\\"methods\\\":{}},\\\"userdoc\\\":{\\\"methods\\\":{}}},\\\"settings\\\":{\\\"compilationTarget\\\":{\\\"C:\\\\\\\\source\\\\\\\\blockchain_fg\\\\\\\\mavenplugin\\\\\\\\web3j-maven-plugin\\\\\\\\src\\\\\\\\test\\\\\\\\resources\\\\\\\\Greeter.sol\\\":\\\"greeter\\\"},\\\"evmVersion\\\":\\\"byzantium\\\",\\\"libraries\\\":{},\\\"optimizer\\\":{\\\"enabled\\\":true,\\\"runs\\\":200},\\\"remappings\\\":[]},\\\"sources\\\":{\\\"C:\\\\\\\\source\\\\\\\\blockchain_fg\\\\\\\\mavenplugin\\\\\\\\web3j-maven-plugin\\\\\\\\src\\\\\\\\test\\\\\\\\resources\\\\\\\\Greeter.sol\\\":{\\\"keccak256\\\":\\\"0x40fb12fdb6f2622513a1517a8fb2e0161629965765182a857649a0090829e50b\\\",\\\"urls\\\":[\\\"bzzr://b7d12de4853376bc1d6ef0a60da3b0dd3e7d48796471499f1a2022ab12c22fec\\\"]}},\\\"version\\\":1}\"},\"C:\\\\source\\\\blockchain_fg\\\\mavenplugin\\\\web3j-maven-plugin\\\\src\\\\test\\\\resources\\\\Greeter.sol:mortal\":{\"abi\":\"[{\\\"constant\\\":false,\\\"inputs\\\":[],\\\"name\\\":\\\"kill\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}]\",\"bin\":\"608060405234801561001057600080fd5b5060008054600160a060020a0319163317905560a7806100316000396000f300608060405260043610603e5763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166341c0e1b581146043575b600080fd5b348015604e57600080fd5b5060556057565b005b60005473ffffffffffffffffffffffffffffffffffffffff1633141560795733ff5b5600a165627a7a723058207b09ce15070219b57572458912299e15b71e581ae363e807f8366091f5ab52a50029\",\"metadata\":\"{\\\"compiler\\\":{\\\"version\\\":\\\"0.4.24+commit.6ae8fb59\\\"},\\\"language\\\":\\\"Solidity\\\",\\\"output\\\":{\\\"abi\\\":[{\\\"constant\\\":false,\\\"inputs\\\":[],\\\"name\\\":\\\"kill\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}],\\\"devdoc\\\":{\\\"methods\\\":{}},\\\"userdoc\\\":{\\\"methods\\\":{}}},\\\"settings\\\":{\\\"compilationTarget\\\":{\\\"C:\\\\\\\\source\\\\\\\\blockchain_fg\\\\\\\\mavenplugin\\\\\\\\web3j-maven-plugin\\\\\\\\src\\\\\\\\test\\\\\\\\resources\\\\\\\\Greeter.sol\\\":\\\"mortal\\\"},\\\"evmVersion\\\":\\\"byzantium\\\",\\\"libraries\\\":{},\\\"optimizer\\\":{\\\"enabled\\\":true,\\\"runs\\\":200},\\\"remappings\\\":[]},\\\"sources\\\":{\\\"C:\\\\\\\\source\\\\\\\\blockchain_fg\\\\\\\\mavenplugin\\\\\\\\web3j-maven-plugin\\\\\\\\src\\\\\\\\test\\\\\\\\resources\\\\\\\\Greeter.sol\\\":{\\\"keccak256\\\":\\\"0x40fb12fdb6f2622513a1517a8fb2e0161629965765182a857649a0090829e50b\\\",\\\"urls\\\":[\\\"bzzr://b7d12de4853376bc1d6ef0a60da3b0dd3e7d48796471499f1a2022ab12c22fec\\\"]}},\\\"version\\\":1}\"}},\"version\":\"0.4.24+commit.6ae8fb59.Windows.msvc\"}\n";

        Map<String, Object> stringObjectMap = jsonParser.parseJson(exampleJson);

        assertTrue(stringObjectMap.containsKey("contracts"));
    }

}