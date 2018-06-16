pragma solidity ^0.4.19;

/**
 * 
 * version 0.3 
 */
contract Predictor {

    /* this runs when the contract is executed */
    constructor() public {
        // initialize the variables
    }

    // returns an int array by adding to all its elements a scalar value "a"
    function subtractScalar(int[] self, int a) public pure returns (int[] s) {
        s = new int[](self.length);
        for (uint i = 0; i < self.length; i++)
            s[i] = self[i] - a;
    }

} // contract Predictor