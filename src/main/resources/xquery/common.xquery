xquery version "1.0";

module namespace ns1 = "http://ns1"; 

declare function ns1:localNum($rows, $k) as element()* {
    let $row := $rows/*
    for $name in distinct-values($row/schema),
        $val in distinct-values($row/*[name()=$k])
    let $count := count($row[schema=$name][*/.[name()=$k]/text()=$val])
    where $count != 0
    order by number($val)
    return (
        <item>
            <schema>{$name}</schema>
            { element {$k} {$val} }
            <count>{$count}</count>
        </item>
   )
};

declare function ns1:globalNum($rows, $k) as element()* {
    let $row := $rows/*
    for $val in distinct-values($row/*[name()=$k])
    let $sum := sum($row[*/.[name()=$k]/text()=$val]/count/text())
    order by number($val)
    return (
        <item>
            { element {$k} {$val} }
            <count>{$sum}</count>
        </item>
   )
};

declare function ns1:wrap($context) as element()* {
    <items>
        { $context }
    </items>
};