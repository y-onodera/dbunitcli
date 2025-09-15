import { describe, expect, it } from 'vitest';
import { isSqlRelatedType, type QueryDatasourceType } from '../../model/QueryDatasource';

describe('QueryDatasourceのテスト', () => {
    describe('isSqlRelatedType関数のテスト', () => {
        it('SQLに関連するタイプでtrueを返すことを確認', () => {
            expect(isSqlRelatedType('sql')).toBe(true);
            expect(isSqlRelatedType('table')).toBe(true);
            expect(isSqlRelatedType('csvq')).toBe(true);
        });

        it('SQLに関連しないタイプでfalseを返すことを確認', () => {
            expect(isSqlRelatedType('xlsx')).toBe(false);
            expect(isSqlRelatedType('csv')).toBe(false);
            expect(isSqlRelatedType('json')).toBe(false);
            expect(isSqlRelatedType('')).toBe(false);
            expect(isSqlRelatedType('unknown')).toBe(false);
        });

        it('型ガードが正しく動作することを確認', () => {
            const testType1: string = 'sql';
            const testType2: string = 'unknown';

            if (isSqlRelatedType(testType1)) {
                // testType1はQueryDatasourceTypeとして扱われる
                const datasourceType: QueryDatasourceType = testType1;
                expect(datasourceType).toBe('sql');
            }

            if (isSqlRelatedType(testType2)) {
                // この分岐には入らない
                expect.fail('unknownはSQL関連タイプではないため、この分岐に入るべきではない');
            }

            expect(isSqlRelatedType(testType2)).toBe(false);
        });
    });
});