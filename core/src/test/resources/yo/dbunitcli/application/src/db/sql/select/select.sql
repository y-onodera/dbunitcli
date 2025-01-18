SELECT
*
FROM
DOCUMENT
$if(param.id)$WHERE ID = $param.id$$endif$