pragma solidity ^0.4.23;

import "./sub/Util.sol";

contract Main {
    address creator;

    constructor () {
        creator = msg.sender;
    }

}
