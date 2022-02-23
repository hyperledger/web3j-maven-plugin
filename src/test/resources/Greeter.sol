// SPDX-License-Identifier: MIT
pragma solidity >=0.5.2 <0.9.0;

/* Example from https://www.ethereum.org/greeter */
contract mortal {
    /* Define variable owner of the type address*/
    address owner;

    /* this function is executed at initialization and sets the owner of the contract */
    constructor () {owner = msg.sender;}

    /* Function to recover the funds on the contract */
    function kill() public {if (msg.sender == owner) selfdestruct(payable(owner));}
}


contract greeter is mortal {
    /* define variable greeting of the type string */
    string greeting;

    /* this runs when the contract is executed */
    constructor(string memory _greeting) {
        greeting = _greeting;
    }

    /* main function */
    function greet() public view returns (string memory) {
        return greeting;
    }
}