pragma solidity >=0.4.19;

contract Issue17import1 {
    address creator;

    constructor() public{
        creator = msg.sender;
    }

}
