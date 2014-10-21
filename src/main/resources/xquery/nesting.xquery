xquery version "1.0";

import module namespace ns1 = "http://ns1" at "common.xquery";

<items>
{
    let $depthLocal := ns1:localNum(./items, "depth")
    let $depthGlobal := ns1:globalNum(ns1:wrap($depthLocal), "depth")
    return (
        element { "depthlocal" } { $depthLocal },
        element { "depthglobal" } { $depthGlobal }
    )
}
</items>