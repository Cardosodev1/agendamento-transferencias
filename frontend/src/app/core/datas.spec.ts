import { hojeIso } from './datas';

describe('hojeIso', () => {
  it('deve formatar a data local como yyyy-MM-dd', () => {
    expect(hojeIso(new Date(2026, 0, 5, 23, 59))).toBe('2026-01-05');
  });
});
