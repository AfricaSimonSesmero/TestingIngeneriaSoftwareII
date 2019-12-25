package edu.uclm.esi.iso2.banco20193capas;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uclm.esi.iso2.banco20193capas.exceptions.ClienteNoAutorizadoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaInvalidaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.CuentaSinTitularesException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.ImporteInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.PinInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.SaldoInsuficienteException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TarjetaBloqueadaException;
import edu.uclm.esi.iso2.banco20193capas.exceptions.TokenInvalidoException;
import edu.uclm.esi.iso2.banco20193capas.model.Cliente;
import edu.uclm.esi.iso2.banco20193capas.model.Cuenta;
import edu.uclm.esi.iso2.banco20193capas.model.Manager;
import edu.uclm.esi.iso2.banco20193capas.model.MovimientoCuenta;
import edu.uclm.esi.iso2.banco20193capas.model.MovimientoTarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaCredito;
import edu.uclm.esi.iso2.banco20193capas.model.TarjetaDebito;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCuentaConFixtures extends TestCase {
	private Cuenta cuentaPepe, cuentaAna;
	private Cliente pepe, ana;
	private TarjetaDebito tdPepe, tdAna;
	private TarjetaCredito tcPepe, tcAna;
	
	@Before
	public void setUp() {
		Manager.getMovimientoDAO().deleteAll();
		Manager.getMovimientoTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaCreditoDAO().deleteAll();
		Manager.getTarjetaDebitoDAO().deleteAll();
		Manager.getCuentaDAO().deleteAll();
		Manager.getClienteDAO().deleteAll();

		this.pepe = new Cliente("12345X", "Pepe", "Pérez");
		this.pepe.insert();
		this.ana = new Cliente("98765F", "Ana", "López");
		this.ana.insert();
		this.cuentaPepe = new Cuenta(1);
		this.cuentaAna = new Cuenta(2);
		
		
		try {
			this.cuentaPepe.addTitular(pepe);
			this.cuentaPepe.insert();
			this.cuentaPepe.ingresar(1000);
			this.cuentaAna.addTitular(ana);
			this.cuentaAna.insert();
			this.cuentaAna.ingresar(5000);
			this.tcPepe = this.cuentaPepe.emitirTarjetaCredito(pepe.getNif(), 2000);
			this.tcPepe.cambiarPin(this.tcPepe.getPin(), 1234);
			this.tcAna = this.cuentaAna.emitirTarjetaCredito(ana.getNif(), 10000);
			this.tcAna.cambiarPin(this.tcAna.getPin(), 1234);
			this.tdPepe = this.cuentaPepe.emitirTarjetaDebito(pepe.getNif());
			this.tdPepe.cambiarPin(this.tdPepe.getPin(), 1234);
			this.tdAna = this.cuentaAna.emitirTarjetaDebito(ana.getNif());
			this.tdAna.cambiarPin(this.tdAna.getPin(), 1234);
		} catch (Exception e) {
			fail("Excepción inesperada en setUp(): " + e);
		}
	}
	
	@Test
	  public void testLiquitado() {
		
		MovimientoTarjetaCredito mtcPepe = new MovimientoTarjetaCredito (tcPepe, 1, "Movimiento");
		try {
			assertTrue(mtcPepe.isLiquidado()==false);
	    } catch (Exception e) {
	      fail("Excepción inesperada: " + e.getMessage());
	    }
	  }
	@Test
	  public void testRetiroForzono() {
		
		try {
			cuentaPepe.transferir(cuentaAna.getId(), 1500, "Transferencia");
			assertTrue(cuentaPepe.getSaldo()==1000);
		} catch (SaldoInsuficienteException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	  }
	
	@Test
	public void testTransferenciaCuentaInvalida() {
		try {
			cuentaPepe.transferir(cuentaPepe.getId(), 1, "Transferencia");
			assertTrue(cuentaPepe.getId()==-1);
		} catch (CuentaInvalidaException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testErrorCompraPorInternetTD() {

		try {
			tdPepe.cambiarPin(tdPepe.getPin(), 9998);
			int token = tdPepe.comprarPorInternet(tdPepe.getPin(), 1);
			tdPepe.confirmarCompraPorInternet(token);
		} catch (TokenInvalidoException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCompraTCSaldoInsuficiente() {
		try {
			tdPepe.cambiarPin(tdPepe.getPin(), 9998);
			tcPepe.comprar(tcPepe.getPin(),1500);
			assertTrue(cuentaPepe.getSaldo()==1000);
		} catch (SaldoInsuficienteException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}	
	}
	
	@Test
	public void testCompraTDPinInvalido() {
		try {
			tdPepe.comprar(10000,1);
			assertTrue(tdPepe.getPin()==10000);
		} catch (PinInvalidoException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}	
	}
	
	@Test
	public void testCompraTDImporteIncorrecto() {
		try {
			tdPepe.cambiarPin(tdPepe.getPin(), 9998);
			tdPepe.comprar(tdPepe.getPin(),-1);
			assertTrue(cuentaPepe.getSaldo()==-1);
		} catch (ImporteInvalidoException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarPinIncorrectoTC() {
		try {
			tcPepe.cambiarPin(-1, 9998);
			assertTrue(tcPepe.getPin()==-1);
		} catch (PinInvalidoException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testCambiarPinIncorrectoTD() {
		try {
			tdPepe.cambiarPin(-1, 9998);
			assertTrue(tdPepe.getPin()==-1);
		} catch (PinInvalidoException e) {
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testIngresarImporteInvalido () {
		try {
			cuentaPepe.ingresar(-1);
			fail("Esperaba ImpoteInvalidoException");
		} catch (ImporteInvalidoException e) {
		}
	}
	
	@Test
	public void testSacarDineroTCImporteInvalido() {
		try {
			tcPepe.cambiarPin(tcPepe.getPin(), 9998);
			tcPepe.sacarDinero(9998, -1);
			fail("Esperaba ImporteInvalidoException");
		} catch (PinInvalidoException | ImporteInvalidoException | SaldoInsuficienteException | TarjetaBloqueadaException e) {
		}
	}
	@Test
	public void testSacarDineroTDImporteInvalido() {
		try {
			tdPepe.cambiarPin(tdPepe.getPin(), 9998);
			tdPepe.sacarDinero(9998, -1);
			fail("Esperaba ImporteInvalidoException");
		} catch (PinInvalidoException | ImporteInvalidoException | SaldoInsuficienteException | TarjetaBloqueadaException e) {
		}
	}
	
	
	@Test
	public void testBloqueoDeTarjetaTD() {
			try {
				this.tdPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			} 
			try {
				this.tdPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tdPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tdPepe.comprarPorInternet(tdPepe.getPin(),1);
			} catch (TarjetaBloqueadaException e) {
			} catch (Exception e) {
				fail("Esperaba TarjetaBloqueadaException");
			}
	}
	
	
	@Test
	public void testBloqueoDeTarjetaComprarTD() {
			try {
				this.tdPepe.comprar(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			} 
			try {
				this.tdPepe.comprar(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tdPepe.comprar(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tdPepe.comprar(tdPepe.getPin(),1);
			} catch (TarjetaBloqueadaException e) {
			} catch (Exception e) {
				fail("Esperaba TarjetaBloqueadaException");
			}
	}
	
	
	@Test
	public void testBloqueoDeTarjetaComprarTC() {
			try {
				this.tcPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			} 
			try {
				this.tcPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tcPepe.comprarPorInternet(9998, 1);
			} catch (PinInvalidoException e) {
			} catch (Exception e) {
				fail("Esperaba PinInvalidoException");
			}
			try {
				this.tcPepe.comprarPorInternet(tcPepe.getPin(),1);
			} catch (TarjetaBloqueadaException e) {
			} catch (Exception e) {
				fail("Esperaba TarjetaBloqueadaException");
			}
	}
	
	@Test
	public void testComprarInternetTDNoAutorizado(){
	
		try {
			TarjetaDebito tdebitoPepe = cuentaPepe.emitirTarjetaDebito("98765F");
			int token = tdebitoPepe.comprarPorInternet(tdebitoPepe.getPin(), 1);
			fail("Se esperaba ClienteNoAutorizadoException");
		} catch(ClienteNoAutorizadoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	
	@Test
	public void testComprarInternetTCNoAutorizado(){
	
		try {
			TarjetaDebito tcreditoPepe = cuentaPepe.emitirTarjetaDebito("98765F");
			int token = tcreditoPepe.comprarPorInternet(tcreditoPepe.getPin(), 1);
			fail("Se esperaba ClienteNoAutorizadoException");
		} catch(ClienteNoAutorizadoException e){
		} catch (Exception e) {
			fail("Excepción inesperada: " + e.getMessage());
		}
	}
	 
}
