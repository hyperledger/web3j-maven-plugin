pragma solidity ^0.4.18;

import "../Upper.sol";

contract Util {
    address creator;

    function Util() {
        creator = msg.sender;
    }
}
