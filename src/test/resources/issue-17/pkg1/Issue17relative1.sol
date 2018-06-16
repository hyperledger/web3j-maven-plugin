pragma solidity ^0.4.19;

import "../Issue17relative2.sol";

contract Issue17relative1 {
    address creator;

    constructor () {
        creator = msg.sender;
    }
}
