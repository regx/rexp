xquery version "1.0";

declare variable $IN_DOC := "schema";
declare variable $IN_SORE := "sore";
declare variable $IN_SOREU := "soreu";
declare variable $IN_CHARE := "cchare";
declare variable $IN_CHARE1 := "chare";
declare variable $IN_SOCHARE := "sochare";
declare variable $IN_EXP := "exp";
declare variable $IN_FORM := "form";
declare variable $IN_CLASS := "class";
declare variable $IN_WCHARE := "wchare";
declare variable $IN_CWCHARE := "cwchare";

declare variable $OUT_ITEM := "item";
declare variable $OUT_ITEMS := "items";

declare variable $OUT_DOC := "schema";
declare variable $OUT_SORE := "sore";
declare variable $OUT_SOREU := "soreu";
declare variable $OUT_CHARE := "cchare";
declare variable $OUT_CHARE1 := "chare";
declare variable $OUT_SOCHARE := "sochare";
declare variable $OUT_EXP := "exp";
declare variable $OUT_FORM := "form";
declare variable $OUT_COUNT := "count";
declare variable $OUT_CLASS := "class";
declare variable $OUT_WCHARE := "wchare";
declare variable $OUT_CWCHARE := "cwchare";

declare function local:localRet($context) as element()* {
    let $row := $context/*
    for $name in distinct-values($row/*[name()=$IN_DOC]/text())
	let $sore := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_SORE]/text()="true"])
	let $soreu := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_SOREU]/text()="true"])
	let $chare := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_CHARE]/text()="true"])
	let $chare1 := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_CHARE1]/text()="true"])
	let $wchare := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_WCHARE]/text()="true"])
	let $cwchare := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_CWCHARE]/text()="true"])
	let $sochare := count($row[*/.[name()=$IN_DOC]/text()=$name][*/.[name()=$IN_CHARE]/text()="true"][*/.[name()=$IN_SORE]/text()="true"])
    let $num := count($row[*/.[name()=$IN_DOC]/text()=$name])
    return (
        element {$OUT_ITEM} {
            element {$OUT_DOC} {$name},
			element {$OUT_SORE} {$sore},
			element {$OUT_SOREU} {$soreu},
			element {$OUT_CHARE} {$chare},
			element {$OUT_CHARE1} {$chare1},
			element {$OUT_WCHARE} {$wchare},
			element {$OUT_CWCHARE} {$cwchare},
			element {$OUT_SOCHARE} {$sochare},
            element {$OUT_EXP} {$num}
        }
   )
};

declare function local:globalRet($context) as element()* {
    let $row := $context/*
    let $sore := sum($row/*[name()=$IN_SORE]/text())
	let $soreu := sum($row/*[name()=$IN_SOREU]/text())
    let $chare := sum($row/*[name()=$IN_CHARE]/text())
    let $chare1 := sum($row/*[name()=$IN_CHARE1]/text())
    let $wchare := sum($row/*[name()=$IN_WCHARE]/text())
    let $cwchare := sum($row/*[name()=$IN_CWCHARE]/text())
	let $sochare := sum($row/*[name()=$IN_SOCHARE]/text())
    let $num := sum($row/*[name()=$IN_EXP]/text())
    return (
        element {$OUT_ITEM} {
			element {$OUT_SORE} {$sore},
			element {$OUT_SOREU} {$soreu},
			element {$OUT_CHARE} {$chare},
			element {$OUT_CHARE1} {$chare1},
			element {$OUT_WCHARE} {$wchare},
			element {$OUT_CWCHARE} {$cwchare},
			element {$OUT_SOCHARE} {$sochare},
            element {$OUT_EXP} {$num},
            element {"sorepc"} {(100 * $sore idiv $num)},
            element {"ccharepc"} {(100 * $chare idiv $num)},
            element {"charepc"} {(100 * $chare1 idiv $num)},
            element {"wcharepc"} {(100 * $wchare idiv $num)},
            element {"cwcharepc"} {(100 * $cwchare idiv $num)}
        }
   )
};

declare function local:form($context) as element()* {
    let $row := $context/*
    for $form in distinct-values($row/*[name()=$IN_CLASS]/text())
    let $count := count($row[*/text()=$form])
    (: This seems to be inefficient, only look at distinct values that are flagged. :)
    where count($row[*/.[name()=$IN_CLASS]/text()=$form][*/.[name()=$IN_CHARE1]/text()="true"]) > 0
    order by string-length($form) ascending, $form descending
    return (
        element {$OUT_ITEM} {
            element {$OUT_CLASS} {$form},
			element {$OUT_COUNT} {$count}
        }
    )
};

declare function local:wrap($context) as element()* {
    element {$OUT_ITEMS} { $context }
};


let $c := local:localRet(./items)
let $d := local:globalRet(local:wrap($c))
return (
     element {$OUT_ITEMS} {
        element {"forms"} {
            local:form(./items)
        },
        element {"local"} {
            $c
        },
		element {"global"} {
            $d
        }
     }
)