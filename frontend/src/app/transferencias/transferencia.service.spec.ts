import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TransferenciaService } from './transferencia.service';

describe('TransferenciaService', () => {
  let service: TransferenciaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TransferenciaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve agendar via POST', () => {
    const request = {
      contaOrigem: '1234567890',
      contaDestino: '0987654321',
      valor: 100,
      dataTransferencia: '2026-10-10',
    };

    service.agendar(request).subscribe();

    const req = http.expectOne('/api/transferencias');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('deve listar o extrato via GET', () => {
    service.listarExtrato().subscribe((extrato) => expect(extrato).toEqual([]));

    const req = http.expectOne('/api/transferencias');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('deve simular a taxa enviando valor e data como parametros', () => {
    service.simularTaxa(1000, '2026-10-10').subscribe(({ taxa }) => expect(taxa).toBe(12));

    const req = http.expectOne('/api/transferencias/taxa?valor=1000&dataTransferencia=2026-10-10');
    expect(req.request.method).toBe('GET');
    req.flush({ taxa: 12 });
  });
});
