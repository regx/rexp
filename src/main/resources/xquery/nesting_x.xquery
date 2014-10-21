xquery version "1.0";

import module namespace  ns1 = "http://ns1" at "common.xquery";

<items>
{
    let $depthLocal := ns1:localNum(./items, "depth")
    let $depthGlobal := ns1:globalNum(ns1:wrap($depthLocal), "depth")
    
    let $ntdepthLocal := ns1:localNum(./items, "ntdepth")
    let $ntdepthGlobal := ns1:globalNum(ns1:wrap($ntdepthLocal), "ntdepth")
    
    let $stardepthLocal := ns1:localNum(./items, "stardepth")
    let $stardepthGlobal := ns1:globalNum(ns1:wrap($stardepthLocal), "stardepth")
    
    let $combineddepthLocal := ns1:localNum(./items, "combineddepth")
    let $combineddepthGlobal := ns1:globalNum(ns1:wrap($combineddepthLocal), "combineddepth")
    
    let $parsedepthLocal := ns1:localNum(./items, "parsedepth")
    let $parsedepthGlobal := ns1:globalNum(ns1:wrap($parsedepthLocal), "parsedepth")
    
    return (
        element { "depthlocal" } { $depthLocal },
        element { "depthglobal" } { $depthGlobal },
        
        element { "ntdepthlocal" } { $ntdepthLocal },
        element { "ntdepthglobal" } { $ntdepthGlobal },
        
        element { "stardepthlocal" } { $stardepthLocal },
        element { "stardepthglobal" } { $stardepthGlobal },
        
        element { "combineddepthlocal" } { $combineddepthLocal },
        element { "combineddepthglobal" } { $combineddepthGlobal },
        
        element { "parsedepthlocal" } { $parsedepthLocal },
        element { "parsedepthglobal" } { $parsedepthGlobal }
    )
}
</items>