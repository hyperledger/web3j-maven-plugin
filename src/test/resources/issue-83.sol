
// SPDX-License-Identifier: MIT
pragma solidity >=0.8.1 < 0.9.0;

contract Main {
    address creator;

    constructor () {
        creator = msg.sender;
    }

}
