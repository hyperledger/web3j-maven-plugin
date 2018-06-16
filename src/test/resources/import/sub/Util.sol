pragma solidity ^0.4.18;

import "../Upper.sol";

contract Util {
    address creator;

    constructor() {
        creator = msg.sender;
    }
}
