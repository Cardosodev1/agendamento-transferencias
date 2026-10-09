import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PADRAO_CONTA = /^\d{10}$/;

const ATE_DUAS_CASAS = /^\d+(\.\d{1,2})?$/;

export function maximoDuasCasasDecimais(
  control: AbstractControl<number | null>,
): ValidationErrors | null {
  const valor = control.value;
  if (valor === null || valor === undefined) {
    return null;
  }
  return ATE_DUAS_CASAS.test(String(valor)) ? null : { casasDecimais: true };
}

export function contasDiferentes(origem: string, destino: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const contaOrigem = grupo.get(origem)?.value;
    const contaDestino = grupo.get(destino)?.value;
    return contaOrigem && contaOrigem === contaDestino ? { contasIguais: true } : null;
  };
}
