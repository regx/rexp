xquery version "1.0";

(: input: (TAG, measurer, evaluator, executor, N, nanoseconds, bytes)* :)

declare function local:f($context) as element()* {
    let $d := $context/items
    for $x in distinct-values($d/item/executor)
    return (
        <items executor="{$x}">
        {
            for $n in distinct-values($d/item/N)
            let $e := $d/item[N=$n][executor=$x]
            order by number($n)
            return
              <item>
                <N>{$n}</N>
                {
                    for $v in distinct-values($d/item/evaluator)
                    let $m := avg($e[evaluator=$v]/bytes)
                    let $t := avg($e[evaluator=$v]/nanoseconds)
                    return (
                        element {concat("m_", $v)}{$m},
                        element {concat("t_", $v)}{$t}
                    )
                }
              </item>
        }
        </items>
    )
};

<result>
{
    local:f(.)
}
</result>