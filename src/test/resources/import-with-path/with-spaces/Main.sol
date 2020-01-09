pragma solidity >=0.4.23;

import "dep spaced/Dependency.sol";

contract Main {
    address creator;

    constructor () public {
        creator = msg.sender;
    }

}
