xquery version "1.0";

import module namespace ns1 = "http://ns1" at "common.xquery";

declare variable $IN_DEPTH := "depth";
(: The following line should not be needed :)
declare variable $ns1:OUT_ITEMS := "items";

<items>
{
    let $depthLocal := ns1:localNum(./*[name()=$ns1:OUT_ITEMS], $IN_DEPTH)
    let $depthGlobal := ns1:globalNum(ns1:wrap($depthLocal), $IN_DEPTH)
    return (
        element { "depthlocal" } { $depthLocal },
        element { "depthglobal" } { $depthGlobal }
    )
}
</items>