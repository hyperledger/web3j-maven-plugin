pragma solidity >=0.4.19 <0.6.0;

import "../Upper.sol";

contract Util {
    address creator;

    constructor() public{
        creator = msg.sender;
    }
}
