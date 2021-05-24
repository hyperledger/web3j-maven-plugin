pragma solidity >=0.4.19 < 0.7.0;

interface InterCheck {
    function check() external;
}

contract CheckImpl is InterCheck {
    function check() public override {
        uint256 i = 0;
        i++;
    }
}

