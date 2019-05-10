pragma solidity >=0.4.19 <0.6.0;

import "../Issue17relative2.sol";

contract Issue17relative1 {
    address creator;

    constructor () public {
        creator = msg.sender;
    }
}
