pragma solidity >=0.4.19;

contract Issue17import2 {
    address creator;

    constructor() public {
        creator = msg.sender;
    }

}
