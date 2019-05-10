pragma solidity >=0.4.23 <0.6.0;

import "./sub/Util.sol";

contract Main {
    address creator;

    constructor () public {
        creator = msg.sender;
    }

}
