pragma solidity >=0.4.19 <0.6.0;

interface InterCheck {
    function check() external;
}

contract ChecImpl is InterCheck {
    function check() public {
        uint256 i = 0;
        i++;
    }
}

