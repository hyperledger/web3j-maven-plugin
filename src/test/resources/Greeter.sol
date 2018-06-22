pragma solidity ^0.4.23;

/* Example from https://www.ethereum.org/greeter */
contract mortal {
    /* Define variable owner of the type address*/
    address owner;

    /* this function is executed at initialization and sets the owner of the contract */
    constructor () public {owner = msg.sender;}

    /* Function to recover the funds on the contract */
    function kill() public {if (msg.sender == owner) selfdestruct(owner);}
}


contract greeter is mortal {
    /* define variable greeting of the type string */
    string greeting;

    /* this runs when the contract is executed */
    constructor(string _greeting) public {
        greeting = _greeting;
    }

    /* main function */
    function greet() public constant returns (string) {
        return greeting;
    }
}