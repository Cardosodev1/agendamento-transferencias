import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AgendamentoRequest, SimulacaoTaxa, Transferencia } from './transferencia.model';

@Injectable({ providedIn: 'root' })
export class TransferenciaService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/transferencias';

  agendar(request: AgendamentoRequest): Observable<Transferencia> {
    return this.http.post<Transferencia>(this.url, request);
  }

  listarExtrato(): Observable<Transferencia[]> {
    return this.http.get<Transferencia[]>(this.url);
  }

  simularTaxa(valor: number, dataTransferencia: string): Observable<SimulacaoTaxa> {
    const params = new HttpParams().set('valor', valor).set('dataTransferencia', dataTransferencia);
    return this.http.get<SimulacaoTaxa>(`${this.url}/taxa`, { params });
  }
}
