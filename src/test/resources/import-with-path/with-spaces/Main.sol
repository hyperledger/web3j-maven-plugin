pragma solidity >=0.4.23 <0.6.0;

import "dep spaced/Dependency.sol";

contract Main {
    address creator;

    constructor () public {
        creator = msg.sender;
    }

}
