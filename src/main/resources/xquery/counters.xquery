xquery version "1.0";

import module namespace ns1 = "http://ns1" at "common.xquery";

<items>
{
    let $minLocal := ns1:localNum(./items, "min")
    let $minGlobal := ns1:globalNum(ns1:wrap($minLocal), "min")
    let $maxLocal := ns1:localNum(./items, "max")
    let $maxGlobal := ns1:globalNum(ns1:wrap($maxLocal), "max")
    return (
        element { "minlocal" } { $minLocal },
        element { "minglobal" } { $minGlobal },
        element { "maxlocal" } { $maxLocal },
        element { "maxglobal" } { $maxGlobal }
    )
}
</items>