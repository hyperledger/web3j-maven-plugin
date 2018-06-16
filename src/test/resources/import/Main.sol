pragma solidity ^0.4.19;

import "./sub/Util.sol";

contract Main {
    address creator;

    constructor () {
        creator = msg.sender;
    }

}
