# コーディングスタイル規約

## if文の波括弧

**すべてのif/else/for/whileブロックに波括弧を必ず付けること。**

Biome の `useBlockStatements` ルールで強制されています。

```typescript
// NG: 波括弧なし（ガード節でも禁止）
if (isJdbcField) return null;

// OK: 波括弧あり
if (isJdbcField) {
    return null;
}
```

## 三項演算子のネスト

**三項演算子のネストは禁止。複雑な条件分岐は if/else を使うこと。**

Biome の `noNestedTernary` ルールで強制されています。

```typescript
// NG: 三項演算子のネスト
const value = a ? b ? "x" : "y" : "z";

// OK: if/else で書く
let value: string;
if (a) {
    value = b ? "x" : "y";
} else {
    value = "z";
}
```

## デフォルト値の指定

**デフォルト値には `??`（nullish合体演算子）を優先して使うこと。**

`undefined`/`null` の場合のみデフォルトを適用したい場合は `??`、falsy値すべてにデフォルトを適用したい場合は `||` を使う。

```typescript
// OK: nullish合体演算子
const path = props.path ?? "";

// 注意: || はfalsy（0, "", false）もデフォルト値に置き換えてしまう
const count = props.count || 0; // props.count が 0 でも 0 になってしまう
```

## JSX内の条件レンダリング

JSX内の条件レンダリングは `&&` または1段の三項演算子を使うこと。

```tsx
// OK: && 演算子
{isVisible && <MyComponent />}

// OK: 1段の三項演算子
{isVisible ? <MyComponent /> : null}

// NG: JSX内でのネスト三項（if/elseに書き換える）
{a ? (b ? <X /> : <Y />) : <Z />}
```

JSX内で複雑な条件が必要な場合は、render関数やコンポーネントに切り出すこと。
