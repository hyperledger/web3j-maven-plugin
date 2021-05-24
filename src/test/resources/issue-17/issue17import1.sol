pragma solidity >=0.4.19 < 0.7.0;

contract Issue17import1 {
    address creator;

    constructor() public{
        creator = msg.sender;
    }

}
