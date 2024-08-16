// SPDX-License-Identifier: MIT
pragma solidity >=0.8.1 < 0.9.0;


library Sample {
    function convert(uint amount, uint conversionRate) pure public returns (uint convertedAmount)
    {
        return amount * conversionRate;
    }
}