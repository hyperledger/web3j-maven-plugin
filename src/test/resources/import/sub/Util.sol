pragma solidity >=0.4.19;

import "../Upper.sol";

contract Util {
    address creator;

    constructor() public{
        creator = msg.sender;
    }
}
