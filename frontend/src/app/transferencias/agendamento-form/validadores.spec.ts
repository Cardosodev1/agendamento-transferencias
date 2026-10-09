import { FormControl, FormGroup } from '@angular/forms';
import { contasDiferentes, maximoDuasCasasDecimais } from './validadores';

describe('validadores', () => {
  it.each([
    [10, null],
    [10.5, null],
    [10.55, null],
    [10.555, { casasDecimais: true }],
    [null, null],
  ])('maximoDuasCasasDecimais(%s)', (valor, esperado) => {
    expect(maximoDuasCasasDecimais(new FormControl(valor))).toEqual(esperado);
  });

  it('contasDiferentes deve acusar contas iguais', () => {
    const grupo = new FormGroup(
      { origem: new FormControl('1234567890'), destino: new FormControl('1234567890') },
      { validators: contasDiferentes('origem', 'destino') },
    );

    expect(grupo.hasError('contasIguais')).toBe(true);

    grupo.controls.destino.setValue('0987654321');
    expect(grupo.hasError('contasIguais')).toBe(false);
  });
});
