pragma solidity >=0.4.23;

import "dep/Dependency.sol";

contract Main {
    address creator;

    constructor () public {
        creator = msg.sender;
    }

}
