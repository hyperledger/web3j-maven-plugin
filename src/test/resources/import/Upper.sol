pragma solidity ^0.4.24;

contract Upper {
    address creator;

    constructor () {
        creator = msg.sender;
    }

}
