pragma solidity ^0.4.18;

import "../Issue17relative2.sol";

contract Issue17relative1 {
    address creator;

    function Issue17relative1() {
        creator = msg.sender;
    }
}
