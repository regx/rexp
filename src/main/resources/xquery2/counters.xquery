xquery version "1.0";

import module namespace ns1 = "http://ns1" at "common.xquery";

declare variable $IN_MIN := "min";
declare variable $IN_MAX := "max";
(: The following line should not be needed :)
declare variable $ns1:OUT_ITEMS := "items";

<items>
{
    let $minLocal := ns1:localNum(./*[name()=$ns1:OUT_ITEMS], $IN_MIN)
    let $minGlobal := ns1:globalNum(ns1:wrap($minLocal), $IN_MIN)
    let $maxLocal := ns1:localNum(./*[name()=$ns1:OUT_ITEMS], $IN_MAX)
    let $maxGlobal := ns1:globalNum(ns1:wrap($maxLocal), $IN_MAX)
    return (
        element { "minlocal" } { $minLocal },
        element { "minglobal" } { $minGlobal },
        element { "maxlocal" } { $maxLocal },
        element { "maxglobal" } { $maxGlobal }
    )
}
</items>