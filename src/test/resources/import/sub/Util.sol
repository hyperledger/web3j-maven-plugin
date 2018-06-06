pragma solidity ^0.4.24;

import "../Upper.sol";

contract Util {
    address creator;

    constructor () {
        creator = msg.sender;
    }
}
