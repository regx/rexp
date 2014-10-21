xquery version "1.0";

declare variable $IN_DOC := "schema";
declare variable $IN_SDT := "sdt";
declare variable $IN_EXP := "exp";

declare variable $OUT_ITEM := "item";
declare variable $OUT_ITEMS := "items";

declare variable $OUT_DOC := "schema";
declare variable $OUT_SDT := "sdt";
declare variable $OUT_EXP := "exp";

declare function local:localSdt($context) as element()* {
    let $row := $context/*
    for $name in distinct-values($row/*[name()=$IN_DOC]/text())
    let $sdt := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_SDT]/text()="true"])
    let $num := count($row[*/.[name()=$IN_DOC]/text()=$name])
    return (
        element {$OUT_ITEM} {
            element {$OUT_DOC} {$name},
            element {$OUT_SDT} {$sdt},
            element {$OUT_EXP} {$num}
        }
   )
};

declare function local:globalSdt($context) as element()* {
    let $row := $context/*
    let $sdt := sum($row/*[name()=$IN_SDT]/text())
    let $num := sum($row/*[name()=$IN_EXP]/text())
    return (
        element {$OUT_ITEM} {
            element {$OUT_SDT} {$sdt},
            element {$OUT_EXP} {$num}
        }
   )
};

declare function local:wrap($context) as element()* {
    element {$OUT_ITEMS} { $context }
};


let $c := local:localSdt(./items)
let $d := local:globalSdt(local:wrap($c))
return (
   local:wrap($d)
)