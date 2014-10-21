xquery version "1.0";

module namespace ns1 = "http://ns1";

declare variable $ns1:IN_DOC := "schema";
declare variable $ns1:IN_COUNT := "count";

declare variable $ns1:OUT_ITEM := "item";
declare variable $ns1:OUT_ITEMS := "items";

declare variable $ns1:OUT_DOC := "schema";
declare variable $ns1:OUT_COUNT := "count";

declare function ns1:localNum($rows, $k) as element()* {
    let $row := $rows/*
    for $name in distinct-values($row/*[name()=$ns1:IN_DOC]/text()),
        $val in distinct-values($row/*[name()=$k])
    let $count := count($row[*/.[name()=$ns1:IN_DOC]/text()=$name][*/.[name()=$k]/text()=$val])
    where $count != 0
    order by number($val)
    return (
        element {$ns1:OUT_ITEM} {
            element {$ns1:OUT_DOC} {$name},
            element {$k} {$val},
            element {$ns1:OUT_COUNT} {$count}
        }
   )
};

declare function ns1:globalNum($rows, $k) as element()* {
    let $row := $rows/*
    for $val in distinct-values($row/*[name()=$k])
    let $sum := sum($row[*/.[name()=$k]/text()=$val]/*[name()=$ns1:IN_COUNT]/text())
    order by number($val)
    return (
        element {$ns1:OUT_ITEM} {
            element {$k} {$val},
            element {$ns1:OUT_COUNT} {$sum}
        }
   )
};

declare function ns1:wrap($context) as element()* {
    element {$ns1:OUT_ITEMS} { $context }
};